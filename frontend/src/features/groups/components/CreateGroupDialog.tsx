import { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Spinner } from '@/components/spinner'
import { IconWarn } from '@/components/icons'
import { useCreateGroupMutation } from '../hooks/useGroups'

/**
 * 새 그룹 생성 다이얼로그.
 * 필드 2개(이름/설명)뿐이라 useState 로 관리. 이름만 필수.
 */
export function CreateGroupDialog({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const create = useCreateGroupMutation()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  const reset = () => {
    setName('')
    setDescription('')
    create.reset()
  }

  const handleOpenChange = (next: boolean) => {
    if (!next) reset()
    onOpenChange(next)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (name.trim().length === 0 || create.isPending) return
    create.mutate(
      { name: name.trim(), description: description.trim() || undefined },
      {
        onSuccess: () => {
          reset()
          onOpenChange(false)
        },
      },
    )
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-[480px]">
        <DialogHeader>
          <DialogTitle>新しいグループを作成</DialogTitle>
          <DialogDescription>グループは店舗単位で作成することをおすすめします。</DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4 py-2">
          <div className="flex flex-col gap-2">
            <Label htmlFor="group-name" className="text-[13px] font-semibold text-muted-foreground">
              グループ名
            </Label>
            <Input
              id="group-name"
              placeholder="例: 渋谷店"
              value={name}
              onChange={(e) => setName(e.target.value)}
              maxLength={100}
              autoFocus
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="group-desc" className="text-[13px] font-semibold text-muted-foreground">
              説明（任意）
            </Label>
            <Textarea
              id="group-desc"
              placeholder="アルバイトに表示される店舗紹介"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              maxLength={255}
            />
          </div>

          {create.isError && (
            <div className="flex items-center gap-2 rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]">
              <IconWarn size={16} stroke={2} />
              {create.error.message}
            </div>
          )}

          <DialogFooter className="mt-2">
            <Button type="button" variant="ghost" onClick={() => handleOpenChange(false)}>
              キャンセル
            </Button>
            <Button type="submit" disabled={name.trim().length === 0 || create.isPending} className="font-bold">
              {create.isPending ? (
                <>
                  <Spinner /> 作成中…
                </>
              ) : (
                '作成'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
